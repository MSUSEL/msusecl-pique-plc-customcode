_help="""Python script for converting given CVEs to their corresponding CWEs using the a local copy of the NVD.
If there is no corresponding CWE or the CWE is CWE-Other the CWE will be CWE-unknown. The script
also contains functionality for converting a GHSA to its corresponding CWE. In order to do this
a Github token must be given as an argument.

Command line arguments
--list (-l): a string of CVEs seperated by commas ie; CVE-2020-123,CVE-2022-456,CVE-2018-789
--github_token (-g): a filepath pointing to a .txt file containing a github token on a single line. This is only needed if there are GHSA IDs to convert to CWEs.
--nvdDict (-n): a filepath pointing to a .json file containing a downloaded version of the NVD saved as a dictionary with CVE IDs as keys.
"""

import argparse
import requests
import json


def ghsa_to_cwe(ghsa, github_token):
    query = f"""query {{
        securityAdvisory(ghsaId: "{ghsa}") {{
            ghsaId
            summary
            cwes(first : 1) {{ nodes {{ cweId }} }}
        }}
    }}"""
    if github_token == '':
        print("Error - GHSA ID present in vulnerabilities to process but no Github token was given.",
              "In order to process GHSA IDs a Github token is needed. Use --help for more information")

    response = requests.post(url='https://api.github.com/graphql', json={'query': query}, headers={'Authorization': 'token %s' % github_token})
    if response.status_code != 200:
        return "Bad Request - " + str(response.status_code)
    else:
        ghsa_data = response.json()
        result = []
        if len(ghsa_data['data']['securityAdvisory']['cwes']['nodes']) > 0:
            for node in ghsa_data['data']['securityAdvisory']['cwes']['nodes']:
                result.append(ghsa_data['data']['securityAdvisory']['cwes']['nodes'][0]['cweId'])
            return result
        else:
            result.append("CWE-unknown")
            return result

def get_cwe_api_direct(cve, github_token, api_key):
    if cve[:4] == "GHSA":
        return ghsa_to_cwe(cve, github_token)

    url = f"https://services.nvd.nist.gov/rest/json/cves/2.0?cveId={cve}"

    if api_key == '':
        response = requests.get(url=url)
    else:
        response = requests.get(url=url, headers={"apiKey" : api_key})

    if response.status_code != 200:
        return "Bad Request - " + str(response.status_code)
    else:
        data = response.json() #json madness
        if len(data['vulnerabilities']) != 0 and 'cve' in data['vulnerabilities'][0] and 'weaknesses' in data['vulnerabilities'][0]['cve']:
            for w in data['vulnerabilities'][0]['cve']['weaknesses']:
                if 'description' in w and len(w['description']) != 0:
                    if w['description'][0]['value'] == "NVD-CWE-noinfo" or w['description'][0]['value'] == "NVD-CWE-Other":
                        return "CWE-unknown"
                    return w['description'][0]['value']

    return "CWE-unknown"


    return True


def get_cwe(cve, github_token, nvd_dict):
    if cve[:4] == "GHSA":
        return ghsa_to_cwe(cve, github_token)

    result = []
    if cve in nvd_dict:
        if 'weaknesses' in nvd_dict[cve]:
            for w in nvd_dict[cve]['weaknesses'][:1]:
                cwe = w['description'][0]['value']
                if cwe == 'NVD-CWE-Other' or cwe == 'NVD-CWE-noinfo':
                    result.append('CWE-unknown')
                else:
                    result.append(cwe)
        else:
            result.append('CWE-unknown')

    return result

def get_cwe_for_cves_api_direct(cve_list, github_token, nvd_key):
    results = []
    for cve in cve_list:
        if cve[:4] == "GHSA":
            cve = '-'.join(cve.split("-", 4)[:4])
        else:
            cve = '-'.join(cve.split("-", 3)[:3])

        cwe = get_cwe_api_direct(cve, github_token, nvd_key)
        results.append(cwe)

    return results

def get_cwe_for_cves(cve_list, github_token, nvd_dict):
    results = []
    for cve in cve_list:
        if cve[:4] == "GHSA":
            cve = '-'.join(cve.split("-", 4)[:4])
        else:
            cve = '-'.join(cve.split("-", 3)[:3])

        cwe = get_cwe(cve, github_token, nvd_dict=nvd_dict)
        results.extend(cwe)

    return results



def main():
    parser = argparse.ArgumentParser(add_help=False)
    parser.add_argument("-l", "--list", dest="cve_list", default="", help="CVE List")
    parser.add_argument("-g", "--github_token", dest="github_token", default="", help="Github Token File Path")
    parser.add_argument("-n", "--nvdDict", dest="nvd_dict", default="", help="NVD Dictionary File Path")
    parser.add_argument("-api", "--use_api", dest="api_key", default="", help="argument to determine whether to skip the nvd dictionary or query the NVD api directly. If using the NVDapi directly supply an nvd key following the arg")

    args = parser.parse_args()
    cves = args.cve_list.split(',')
    github_token_path = args.github_token
    nvd_dict_path = args.nvd_dict
    nvd_api_key_path = args.api_key

    # try github token file
    try:
        with open(github_token_path) as f:
            github_token = f.readline().rstrip()
    except Error as e:
        print(f"Error - opening github token{e}")
        exit(1)

    result = []
    if nvd_api_key_path != "":
        try:
            with open(nvd_api_key_path) as f:
                nvd_api_key = f.readline().rstrip()
        except Error as e:
            print(f"Error - opening nvd api key{e}")
            exit(1)
        result = get_cwe_for_cves_api_direct(cves, github_token, nvd_api_key)
    else:
        with open(nvd_dict_path, "r") as json_file:
            nvd_dict = json.load(json_file)
        result = get_cwe_for_cves(cves, github_token, nvd_dict)


    # LOLOL super bad tech debt, some of the worst I have seen. If you remove this, the program fails.
    # The java end of things parses this content from stdout. It is a workaround to having the python side of things return a list.
    for c in result:
        print(c)
        print(" ")

main()
